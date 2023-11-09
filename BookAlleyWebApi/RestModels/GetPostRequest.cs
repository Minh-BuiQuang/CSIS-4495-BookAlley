namespace BookAlleyWebApi.RestModels
{
    public class GetPostRequest
    {
        public Guid? SessionToken { get; set; }
        public string? Location { get; set; }
        public string? Keyword { get; set; }
    }
}
